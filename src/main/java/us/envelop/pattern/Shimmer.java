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
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Meters")
public class Shimmer extends EnvelopPattern {

  private final int BUFFER_SIZE = 512;
  private final float[][] buffer = new float[NUM_COLUMNS][BUFFER_SIZE];
  private int bufferPos = 0;

  public final CompoundParameter interp = new CompoundParameter("Mode", 0);

  public final CompoundParameter speed = new CompoundParameter("Speed", 1, 5, .1)
  .setDescription("Speed of the sound waves emanating from the speakers");

    public final CompoundParameter taper = new CompoundParameter("Taper", 1, 0, 10)
    .setExponent(2)
    .setDescription("Amount of tapering applied to the signal");

  private final DampedParameter speedDamped = new DampedParameter(speed, 1);

  public Shimmer(LX lx) {
    super(lx);
    addParameter("intern", interp);
    addParameter("speed", speed);
    addParameter("taper", taper);
    startModulator(speedDamped);
    for (float[] buffer : this.buffer) {
      for (int i = 0; i < buffer.length; ++i) {
        buffer[i] = 0;
      }
    }
  }

  @Override
  public void run(double deltaMs) {
    float speed = this.speedDamped.getValuef();
    float interp = this.interp.getValuef();
    float taper = this.taper.getValuef() * LXUtils.lerpf(3, 1, interp);
    int columnIndex = 0;
    for (LXModel column : getColumns()) {
      float[] buffer = this.buffer[columnIndex];
      buffer[this.bufferPos] = lx.engine.audio.envelop.decode.channels[columnIndex].getValuef();
      for (LXModel rail : column.sub("rail")) {
        for (int i = 0; i < rail.points.length; ++i) {
          LXPoint p = rail.points[i];
          int i3 = i % (rail.points.length/3);
          float td = Math.abs(i3 - rail.points.length / 6);
          float threeWay = getValue(buffer, speed * td);
          float nd = Math.abs(i - rail.points.length / 2);
          float normal = getValue(buffer, speed * nd);
          float bufferValue = LXUtils.lerpf(threeWay, normal, interp);
          float d = LXUtils.lerpf(td, nd, interp);
          colors[p.index] = LXColor.gray(LXUtils.maxf(0, 100 * bufferValue - d*taper));
        }
      }
      ++columnIndex;
    }
    --bufferPos;
    if (bufferPos < 0) {
      bufferPos = BUFFER_SIZE - 1;
    }
  }

  private float getValue(float[] buffer, float bufferOffset) {
    int offsetFloor = (int) bufferOffset;
    int bufferTarget1 = (bufferPos + offsetFloor) % BUFFER_SIZE;
    int bufferTarget2 = (bufferPos + offsetFloor + 1) % BUFFER_SIZE;
    return LXUtils.lerpf(buffer[bufferTarget1], buffer[bufferTarget2], bufferOffset - offsetFloor);
  }
}