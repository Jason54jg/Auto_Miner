package com.jason54jg.miningmod.client

import com.jason54jg.miningmod.XrayScanner
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OrderedSubmitNodeCollector
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.shapes.Shapes

/**
 * Contours "à travers les murs" des minerais repérés par [XrayScanner].
 *
 * ATTENTION — fichier expérimental et réécrit une deuxième fois : Minecraft 26.2 a
 * carrément supprimé `Tesselator`/l'ancien rendu immédiat, remplacé par un système
 * "submit" (soumettre une forme au moteur, qui la dessine lui-même) via
 * `OrderedSubmitNodeCollector#submitShapeOutline`. C'est le point le plus fragile
 * du mod entier — si le build casse quelque part, commencer par ici.
 */
object XrayRenderer {
    private const val LINE_COLOR = 0xFFFF4040.toInt()
    private const val LINE_WIDTH = 2.0f

    private val PIPELINE: RenderPipeline by lazy {
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("miningmod", "pipeline/xray_lines"))
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
        LevelRenderEvents.COLLECT_SUBMITS.register(::submit)
    }

    private fun submit(context: LevelRenderContext) {
        val positions = XrayScanner.visiblePositions
        if (positions.isEmpty()) return

        val collector = context.submitNodeCollector() as? OrderedSubmitNodeCollector ?: return
        val camera = Minecraft.getInstance().gameRenderer.mainCamera().position()
        val poseStack = context.poseStack()

        for (pos in positions) {
            poseStack.pushPose()
            poseStack.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z)
            collector.submitShapeOutline(poseStack, Shapes.block(), RENDER_TYPE, LINE_COLOR, LINE_WIDTH, true)
            poseStack.popPose()
        }
    }
}
