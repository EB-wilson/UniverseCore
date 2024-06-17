package universecore.ui.elements;

import arc.Core;
import arc.graphics.g2d.Bloom;
import arc.scene.Group;

import static arc.Core.settings;

public class BloomGroup extends Group {
  private final Bloom bloom = new Bloom(true);
  private boolean clip = false;

  public boolean getClip(){
    return clip;
  }

  public void setClip(boolean clip){
    this.clip = clip;
  }

  @Override
  protected void drawChildren() {
    boolean bloomEnabled = settings.getBool("bloom");
    if (bloomEnabled){
      bloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
      bloom.setBloomIntensity(settings.getInt("bloomintensity", 6) / 4f + 1f);
      bloom.blurPasses = settings.getInt("bloomblur", 1);

      bloom.capture();
    }
    if (clip) clipBegin();
    super.drawChildren();
    if (clip) clipEnd();
    if (bloomEnabled) bloom.render();
  }
}
