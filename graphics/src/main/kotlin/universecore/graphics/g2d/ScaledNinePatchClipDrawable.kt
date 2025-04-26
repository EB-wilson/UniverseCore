package universecore.graphics.g2d

import arc.graphics.g2d.NinePatch
import arc.scene.style.NinePatchDrawable
import arc.scene.ui.layout.Scl

open class ScaledNinePatchClipDrawable : NinePatchClipDrawable {
  open val scale get() = Scl.scl(1f)

  /** Creates an uninitialized NinePatchDrawable. The ninepatch must be [setPatch] before use.  */
  constructor(): super()
  constructor(patch: NinePatch): super(patch)
  constructor(drawable: NinePatchClipDrawable): super(drawable)
  constructor(drawable: NinePatchDrawable): super(drawable.patch!!)

  override fun draw(
    x: Float, y: Float, width: Float, height: Float,
    clipLeft: Float, clipRight: Float, clipTop: Float, clipBottom: Float
  ) {
    val scale = scale
    draw(
      x, y, 0f, 0f,
      width/scale, height/scale, scale, scale, 0f,
      clipLeft/scale, clipRight/scale, clipTop/scale, clipBottom/scale
    )
  }

  override fun setPatch(patch: NinePatch) {
    super.setPatch(patch)

    minWidth = patch.totalWidth*scale
    minHeight = patch.totalHeight*scale
  }

  override var leftWidth: Float
    get() = ninePatch.padLeft*scale
    set(_) {}
  override var rightWidth: Float
    get() = ninePatch.padRight*scale
    set(_) {}
  override var topHeight: Float
    get() = ninePatch.padTop*scale
    set(_) {}
  override var bottomHeight: Float
    get() = ninePatch.padBottom*scale
    set(_) {}
}