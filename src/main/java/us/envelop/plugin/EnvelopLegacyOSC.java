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

package us.envelop.plugin;

import java.net.SocketException;

import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.audio.Envelop;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

@LXPlugin.Name("E4L Legacy OSC Support")
public class EnvelopLegacyOSC implements LXPlugin {

  private Envelop envelop;

  private static final void log(String message) {
    LX.log("[Envelop] " + message);
  }

  private static final void error(String message) {
    LX.error("[Envelop] " + message);
  }

  private static final void error(Exception x, String message) {
    LX.error(x, "[Envelop] " + message);
  }

  @Override
  public void initialize(LX lx) {
    this.envelop = lx.engine.audio.envelop;
    try {
      lx.engine.osc.receiver(3366).addListener(new EnvelopOscMeterListener());
      lx.engine.osc.receiver(3377).addListener(new EnvelopOscListener());
      log("Started Legacy Envelop OSC listeners on port 3366/3377");

      // Can't see where these two would send from anymore?
      // lx.engine.osc.receiver(3344).addListener(new EnvelopOscControlListener(lx));
      // lx.engine.osc.receiver(3355).addListener(new EnvelopOscSourceListener());
    } catch (SocketException sx) {
      error(sx, "Failed to start legacy Envelop OSC listeners on port 3366/3377");
    }
  }

  private class EnvelopOscListener implements LXOscListener {

    public void oscMessage(OscMessage message) {
      try {
        String raw = message.getAddressPattern().getValue();
        String trim = raw.trim();
        if (trim != raw) {
          error("Trailing whitespace in OSC address pattern: \"" + raw + "\"");
        }
        String[] parts = trim.split("/");
        if (parts[1].equals(Envelop.ENVELOP_OSC_PATH)) {
          envelop.handleEnvelopOscMessage(message, parts, 1);
        }
      } catch (Exception x) {
        error(x, "Error handling envelop oscMessage: " + message.toString());
      }
    }
  }

  class EnvelopOscMeterListener implements LXOscListener {
    public void oscMessage(OscMessage message) {
      if (message.matches("/server/dsp/meter/input")) {
        // TODO: restore if needed?
        // envelop.source.setLevels(message);
        error("Ignoring EnvelopOscMeter input message: " + message.toString());
      } else if (message.matches("/server/dsp/meter/decoded")) {
        // TODO: restore if needed?
        // envelop.decode.setLevels(message);
        error("Ignoring EnvelopOscMeter decode message: " + message.toString());
      } else {
        error("Unrecognized EnvelopOscMeter message: " + message.toString());
      }
    }
  }

}
