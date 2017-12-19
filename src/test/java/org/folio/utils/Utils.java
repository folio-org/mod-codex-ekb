/**
 *
 */
package org.folio.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * @author mreno
 *
 */
public final class Utils {
  private Utils() {
    super();
  }

  /**
   * Returns a random ephemeral port that has a high probability of not being
   * in use.
   *
   * @return a random port number.
   */
  public static int getRandomPort() {
    int port = -1;
    do {
      // Use a random ephemeral port if not defined via a system property
      port = new Random().nextInt(16_384) + 49_152;
      try {
        ServerSocket socket = new ServerSocket(port);
        socket.close();
      } catch (IOException e) {
        continue;
      }
      break;
    } while (true);

    return port;
  }
}
