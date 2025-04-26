package universecore.graphics.g2d

interface ClipDrawable {
  fun draw(
    x: Float, y: Float,
    width: Float, height: Float,
    clipLeft: Float, clipRight: Float,
    clipTop: Float, clipBottom: Float
  )
  fun draw(
    x: Float, y: Float,
    originX: Float, originY: Float,
    width: Float, height: Float,
    scaleX: Float, scaleY: Float,
    rotation: Float,
    clipLeft: Float, clipRight: Float,
    clipTop: Float, clipBottom: Float
  )
  var leftWidth: Float
  var rightWidth: Float
  var topHeight: Float
  var bottomHeight: Float
  var minWidth: Float
  var minHeight: Float
}
