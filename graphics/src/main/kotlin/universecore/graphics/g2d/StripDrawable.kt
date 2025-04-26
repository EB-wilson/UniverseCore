package helium.graphics

interface StripDrawable {
  fun draw(originX: Float, originY: Float, stripWidth: Float, angleDelta: Float) =
    draw(originX, originY, 0f, 0f, angleDelta, stripWidth)
  fun draw(originX: Float, originY: Float, angle: Float, distance: Float, angleDelta: Float, stripWidth: Float)
  var leftOff: Float
  var rightOff: Float
  var outerWidth: Float
  var innerWidth: Float
  var minOffset: Float
  var minWidth: Float
}