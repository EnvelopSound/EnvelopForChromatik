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
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

@LXCategory("Envelop/Form")
public class Rings extends EnvelopPattern {

  public final CompoundParameter amplitude =
    new CompoundParameter("Amplitude", 1);

  public final CompoundParameter speed = new CompoundParameter("Speed", 10000, 20000, 1000)
  .setExponent(.25);

  public Rings(LX lx) {
    super(lx);
    for (int i = 0; i < 2; ++i) {
      addLayer(new Ring(lx));
    }
    addParameter("amplitude", this.amplitude);
    addParameter("speed", this.speed);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }

  class Ring extends LXLayer {

    private LXProjection proj = new LXProjection(model);
    private final SawLFO yRot = new SawLFO(0, LX.TWO_PIf, 9000 + 2000 * Math.random());
    private final SinLFO zRot = new SinLFO(-1, 1, speed);
    private final SinLFO zAmp = new SinLFO(LX.PIf / 10, LX.PIf / 4, 13000 + 3000 * Math.random());
    private final SinLFO yOffset = new SinLFO(-2*FEET, 2*FEET, 12000 + 5000*Math.random());

    public Ring(LX lx) {
      super(lx);
      startModulator(yRot.randomBasis());
      startModulator(zRot.randomBasis());
      startModulator(zAmp.randomBasis());
      startModulator(yOffset.randomBasis());
    }

    @Override
    public void run(double deltaMs) {
      proj.reset().center().rotateY(yRot.getValuef()).rotateZ(amplitude.getValuef() * zAmp.getValuef() * zRot.getValuef());
      float yOffset = this.yOffset.getValuef();
      float falloff = 100 / (2*FEET);
      for (LXVector v : proj) {
        float b = 100 - falloff * Math.abs(v.y - yOffset);
        if (b > 0) {
          addColor(v.index, LXColor.gray(b));
        }
      }
    }
  }
}
