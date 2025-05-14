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
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Meters")
public class Decode extends EnvelopPattern {

  public final CompoundParameter mode = new CompoundParameter("Mode", 0);
  public final CompoundParameter fade = new CompoundParameter("Fade", 1*FEET, 0.001, 6*FEET);
  public final CompoundParameter damping = new CompoundParameter("Damping", 10, 10, .1)
  .setExponent(.25);

  private final DampedParameter[] dampedDecode =
    new DampedParameter[lx.engine.audio.envelop.decode.channels.length];

  public Decode(LX lx) {
    super(lx);
    addParameter("mode", mode);
    addParameter("fade", fade);
    addParameter("damping", damping);
    int d = 0;
    for (LXParameter parameter : lx.engine.audio.envelop.decode.channels) {
      startModulator(dampedDecode[d++] = new DampedParameter(parameter, damping));
    }
  }

  @Override
  public void run(double deltaMs) {
    float fv = fade.getValuef();
    float falloff = 100 / fv;
    float mode = this.mode.getValuef();
    float faden = fade.getNormalizedf();
    int columnIndex = 0;
    for (LXModel column : getColumns()) {
      float levelf = this.dampedDecode[columnIndex].getValuef();
      float level = levelf * (model.yRange / 2f);
      for (LXModel rail : column.sub("rail")) {
        for (LXPoint p : rail.points) {
          float yn = Math.abs(p.y - model.cy);
          float b0 = LXUtils.constrainf(falloff * (level - yn), 0, 100);
          float b1max = LXUtils.lerpf(100, 100*levelf, faden);
          float b1 = (yn > level) ? LXUtils.maxf(0, b1max - 80*(yn-level)) : LXUtils.lerpf(0, b1max, yn / level);
          colors[p.index] = LXColor.gray(LXUtils.lerpf(b0, b1, mode));
        }
      }
      ++columnIndex;
    }
  }
}
