package helium.graphics

abstract class BaseStripDrawable: StripDrawable {
  override var leftOff: Float = 0f
  override var rightOff: Float = 0f
  override var outerWidth: Float = 0f
  override var innerWidth: Float = 0f
  override var minOffset: Float = 0f
  override var minWidth: Float = 0f
}