package jp.hotdrop.createblogsupporter.data.llm

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmClient
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmRequest
import jp.hotdrop.createblogsupporter.domain.usecase.LlmSupportException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class LiteRtLmBlogSupportClient @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : BlogSupportLlmClient {
    private val engineMutex = Mutex()
    private val generationMutex = Mutex()
    private var currentModelPath: String? = null
    private var engine: Engine? = null

    override fun streamText(request: BlogSupportLlmRequest): Flow<String> = flow {
        generationMutex.withLock {
            val modelPath = resolveModelPath()
            val activeEngine = ensureEngine(modelPath)
            val conversationConfig = ConversationConfig(
                systemInstruction = Contents.of(BaseSystemPrompt),
                samplerConfig = SamplerConfig(
                    topK = 32,
                    topP = 0.9,
                    temperature = request.temperature.toDouble(),
                ),
            )
            try {
                activeEngine.createConversation(conversationConfig).use { conversation ->
                    var rendered = ""
                    conversation.sendMessageAsync(request.prompt).collect { message ->
                        val candidate = extractText(message)
                        if (candidate.isBlank()) return@collect
                        rendered = mergeRenderedText(rendered, candidate)
                        emit(rendered)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: LlmSupportException) {
                throw e
            } catch (e: Exception) {
                throw LlmSupportException(LlmSupportFailure.GenerationFailed, e)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun resolveModelPath(): String {
        val modelFile = File(context.filesDir, DefaultModelRelativePath)
        if (!modelFile.exists() || !modelFile.isFile) {
            throw LlmSupportException(LlmSupportFailure.ModelFileMissing)
        }
        return modelFile.absolutePath
    }

    private suspend fun ensureEngine(modelPath: String): Engine =
        engineMutex.withLock {
            if (currentModelPath == modelPath && engine != null) {
                return@withLock requireNotNull(engine)
            }

            engine?.close()
            val initialized = try {
                createEngine(modelPath, Backend.GPU())
            } catch (e: CancellationException) {
                throw e
            } catch (gpuError: Exception) {
                Log.w(Tag, "Failed to initialize LiteRT-LM with GPU backend. Falling back to CPU.", gpuError)
                try {
                    createEngine(modelPath, Backend.CPU())
                } catch (e: CancellationException) {
                    throw e
                } catch (cpuError: Exception) {
                    throw LlmSupportException(LlmSupportFailure.InitializationFailed, cpuError)
                }
            }

            currentModelPath = modelPath
            engine = initialized
            initialized
        }

    private fun createEngine(modelPath: String, backend: Backend): Engine {
        val cacheDir = File(context.cacheDir, CacheDirectoryName).apply { mkdirs() }
        return Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                cacheDir = cacheDir.absolutePath,
            ),
        ).also { it.initialize() }
    }

    private fun extractText(message: Message): String =
        message.contents.contents
            .mapNotNull { content -> (content as? Content.Text)?.text }
            .joinToString(separator = "")

    private fun mergeRenderedText(previous: String, candidate: String): String =
        when {
            candidate.startsWith(previous) -> candidate
            previous.endsWith(candidate) -> previous
            else -> previous + candidate
        }

    companion object {
        private const val Tag = "LiteRtLmBlogClient"
        private const val CacheDirectoryName = "litertlm-cache"
        private const val DefaultModelRelativePath = "models/blog-supporter.litertlm"
        private const val BaseSystemPrompt =
            "あなたはユーザー自身の言葉でテックブログを書くための執筆支援アシスタントです。" +
                "完成本文を代筆せず、構成、論点整理、改善案、校正候補だけを提案してください。" +
                "提案はユーザーが編集して判断できる候補として返してください。"
    }
}
