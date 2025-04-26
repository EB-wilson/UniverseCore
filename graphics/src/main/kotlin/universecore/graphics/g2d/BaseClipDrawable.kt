package universecore.graphics.g2d

import arc.scene.style.BaseDrawable

abstract class BaseClipDrawable(): ClipDrawable {
  @Suppress("LeakingThis")
  constructor(other: ClipDrawable): this(){
    if (other is BaseDrawable) name =  other.name
    leftWidth = other.leftWidth
    rightWidth = other.rightWidth
    topHeight = other.topHeight
    bottomHeight = other.bottomHeight
    minWidth = other.minWidth
    minHeight = other.minHeight
  }

  var name: String? = null
  override var leftWidth: Float = 0f
  override var rightWidth: Float = 0f
  override var topHeight: Float = 0f
  override var bottomHeight: Float = 0f
  override var minWidth: Float = 0f
  override var minHeight: Float = 0f

  override fun toString(): String {
    if (name == null) return this::class.toString()
    return name!!
  }
}