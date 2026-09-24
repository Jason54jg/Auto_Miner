package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.XrayScanner
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.Identifier

/**
 * Contours "à travers les murs" des minerais repérés par [XrayScanner].
 *
 * ATTENTION — fichier expérimental : Minecraft 26.2 a entièrement réécrit le rendu
 * 3D bas niveau (RenderPipeline/RenderType remplaçant l'ancien système). Impossible
 * de compiler dans ce sandbox pour vérifier ce fichier précisément (pas d'accès
 * réseau à Mojang/Fabric, pas de JDK 25 ici). C'est le point le plus fragile du
 * mod entier — si le build casse quelque part, commencer par ici (le pipeline
 * personnalisé ci-dessous en particulier).
 */
object XrayRenderer {
    private const val LINE_COLOR = 0xFFFF4040.toInt()

    private val PIPELINE: RenderPipeline by lazy {
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("miningmod", "pipeline/xray_lines"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
            .withCull(false)
            .withoutBlend()
            .withDepthTestFunction(RenderPipeline.DepthTestFunction.NO_DEPTH_TEST)
            .build()
    }

    private val RENDER_TYPE: RenderType by lazy {
        RenderType.create("miningmod_xray_lines", 1536, PIPELINE)
    }

    fun register() {
        LevelRenderEvents.END_MAIN.register(::render)
    }

    private fun render(context: LevelRenderContext) {
        val positions = XrayScanner.visiblePositions
        if (positions.isEmpty()) return

        val camera = Minecraft.getInstance().gameRenderer.mainCamera.position
        val matrix = context.poseStack().last().pose()

        val buffer = Tesselator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR)
        for (pos in positions) {
            val x0 = (pos.x - camera.x).toFloat()
            val y0 = (pos.y - camera.y).toFloat()
            val z0 = (pos.z - camera.z).toFloat()
            addBoxOutline(buffer, matrix, x0, y0, z0, x0 + 1f, y0 + 1f, z0 + 1f)
        }
        RENDER_TYPE.draw(buffer.buildOrThrow())
    }

    private fun addBoxOutline(
        buffer: com.mojang.blaze3d.vertex.BufferBuilder,
        matrix: org.joml.Matrix4f,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float
    ) {
        val edges = listOf(
            floatArrayOf(x0, y0, z0, x1, y0, z0), floatArrayOf(x1, y0, z0, x1, y0, z1),
            floatArrayOf(x1, y0, z1, x0, y0, z1), floatArrayOf(x0, y0, z1, x0, y0, z0),
            floatArrayOf(x0, y1, z0, x1, y1, z0), floatArrayOf(x1, y1, z0, x1, y1, z1),
            floatArrayOf(x1, y1, z1, x0, y1, z1), floatArrayOf(x0, y1, z1, x0, y1, z0),
            floatArrayOf(x0, y0, z0, x0, y1, z0), floatArrayOf(x1, y0, z0, x1, y1, z0),
            floatArrayOf(x1, y0, z1, x1, y1, z1), floatArrayOf(x0, y0, z1, x0, y1, z1)
        )
        for (edge in edges) {
            buffer.addVertex(matrix, edge[0], edge[1], edge[2]).setColor(LINE_COLOR)
            buffer.addVertex(matrix, edge[3], edge[4], edge[5]).setColor(LINE_COLOR)
        }
    }
}
