/**
 * Envelop for Chromatik
 * Copyright 2025 Mark C. Slee, Envelop
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * @author Mark C. Slee <mcslee@mcslee.com>
 */

package us.envelop.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Texture")
@LXComponentName("Perlin")
public class Noise extends EnvelopPattern {

  public final CompoundParameter scale =
    new CompoundParameter("Scale", 10, 5, 40);

  private final LXParameter scaleDamped =
    startModulator(new DampedParameter(this.scale, 5, 10));

  public final CompoundParameter floor =
    new CompoundParameter("Floor", 0, -2, 2)
    .setDescription("Lower bound of the noise");

  private final LXParameter floorDamped =
    startModulator(new DampedParameter(this.floor, .5, 2));

  public final CompoundParameter range =
    new CompoundParameter("Range", 1, .2, 4)
    .setDescription("Range of the noise");

  private final LXParameter rangeDamped =
    startModulator(new DampedParameter(this.range, .5, 4));

  public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6)
  .setDescription("Rate of motion on the X-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6)
  .setDescription("Rate of motion on the Y-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6)
  .setDescription("Rate of motion on the Z-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1)
  .setDescription("Offset of symmetry on the X-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1)
  .setDescription("Offset of symmetry on the Y-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1)
  .setDescription("Offset of symmetry on the Z-axis")
  .setPolarity(LXParameter.Polarity.BIPOLAR);

  public Noise(LX lx) {
    super(lx);
    addParameter("scale", this.scale);
    addParameter("floor", this.floor);
    addParameter("range", this.range);
    addParameter("xSpeed", this.xSpeed);
    addParameter("ySpeed", this.ySpeed);
    addParameter("zSpeed", this.zSpeed);
    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
  }

  private class Accum {
    private float accum = 0;
    private int equalCount = 0;

    void accum(double deltaMs, float speed) {
      if (speed != 0) {
        float newAccum = (float) (this.accum + deltaMs * speed * 0.00025);
        if (newAccum == this.accum) {
          if (++this.equalCount >= 5) {
            this.equalCount = 0;
            newAccum = 0;
          }
        }
        this.accum = newAccum;
      }
    }
  };

  private final Accum xAccum = new Accum();
  private final Accum yAccum = new Accum();
  private final Accum zAccum = new Accum();

  @Override
  public void run(double deltaMs) {
    xAccum.accum(deltaMs, xSpeed.getValuef());
    yAccum.accum(deltaMs, ySpeed.getValuef());
    zAccum.accum(deltaMs, zSpeed.getValuef());

    float sf = scaleDamped.getValuef() / 1000f;
    float rf = rangeDamped.getValuef();
    float ff = floorDamped.getValuef();
    float xo = xOffset.getValuef();
    float yo = yOffset.getValuef();
    float zo = zOffset.getValuef();
    for (LXPoint p :  model.points) {
      float b = ff + rf * noise(sf*p.x + xo - xAccum.accum, sf*p.y + yo - yAccum.accum, sf*p.z + zo - zAccum.accum);
      colors[p.index] = LXColor.gray(LXUtils.constrainf(b*100, 0, 100));
    }
  }
}
