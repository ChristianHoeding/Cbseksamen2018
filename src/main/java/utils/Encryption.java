package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better Fixed - Down below we are telling the system where the key is
      // Vi laver en key String "encryptionKey" som vi sætter lig med path til hvor vores nøgle ligger henne.
      String encryptionKey = Config.getEncryptionkeyArray();
      // Nedenstående sætter vi vores char arrray med navnet "key" lig med vores "encryptionkey" som vi laver om til et charArrray.
      char[]key = encryptionKey.toCharArray();

      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();
// Ovenstående oprettes et objekt af Stringbuilder
      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?  - Fixed
      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      }
      // Linje 19 betyder at der skal bygges en Streng af bogstaver, hvor du ligger de binære værdier for hver bogstav i din streng med bogstav fra din char array.

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
