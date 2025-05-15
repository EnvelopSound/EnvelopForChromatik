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

package us.envelop.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.LXWaveshape;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop")
public class Strobe extends LXEffect {

  public enum Waveshape {
    TRI,
    SIN,
    SQUARE,
    UP,
    DOWN
  };

  public final EnumParameter<Waveshape> mode =
    new EnumParameter<Waveshape>("Shape", Waveshape.TRI);

  public final CompoundParameter frequency =
    new CompoundParameter("Freq", 1, .05, 10)
    .setUnits(CompoundParameter.Units.HERTZ);

  public final CompoundParameter depth =
    new CompoundParameter("Depth", 0.5)
    .setDescription("Depth of the strobe effect");

  private final SawLFO basis = new SawLFO(1, 0, new FunctionalParameter() {
    @Override
    public double getValue() {
      return 1000 / frequency.getValue();
  }});

  public Strobe(LX lx) {
    super(lx);
    addParameter("mode", this.mode);
    addParameter("frequency", this.frequency);
    addParameter("depth", this.depth);
    startModulator(basis);
  }

  @Override
  protected void onEnable() {
    basis.setBasis(0).start();
  }

  private LXWaveshape getWaveshape() {
    switch (this.mode.getEnum()) {
    case SIN: return LXWaveshape.SIN;
    case TRI: return LXWaveshape.TRI;
    case UP: return LXWaveshape.UP;
    case DOWN: return LXWaveshape.DOWN;
    case SQUARE: return LXWaveshape.SQUARE;
    }
    return LXWaveshape.SIN;
  }

  @Override
  public void run(double deltaMs, double amount) {
    float amt = this.enabledDamped.getValuef() * this.depth.getValuef();
    if (amt > 0) {
      float strobef = basis.getValuef();
      strobef = (float) getWaveshape().compute(strobef);
      strobef = LXUtils.lerpf(1, strobef, amt);
      if (strobef < 1) {
        if (strobef == 0) {
          for (int i = 0; i < colors.length; ++i) {
            colors[i] = LXColor.BLACK;
          }
        } else {
          int mask = LXColor.gray(strobef * 100);
          for (int i = 0; i < colors.length; ++i) {
            colors[i] = LXColor.multiply(colors[i], mask, LXColor.BLEND_ALPHA_FULL);
          }
        }
      }
    }
  }
}
