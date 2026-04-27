package org.example;

import java.util.Base64;

public class Base64DecoderExample {

    public static void main(String[] args) {
        // Example of a valid Base64 encoded string
        String base64String = "iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAACXBIWXMAAAsSAAALEgHS3X78AAAA"
                + "GXRFWWHRTGluenMgU1ZHLWlDRW8gdmVyc2lvbj1MMS4zAAAAB3RJTUUH5QgUDQMb99GAygAAAAZi"
                + "S0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAA10RVh0U29mdHdhcmUATWFjcm9t"
                + "ZWRpYSBQYWludCAtIEV4cHJlc3MtZi/ANRThAAAAJXRFWHRDb21tZW50AHRlc3QgcmVkIGRvdCB0"
                + "ZW50IGltYWdlIGZpbGUQb34MAAAAaklEQVQoU2NkwAD+EADEOBBSFnQAhtIZeARyBk+BVoF9ABIQ"
                + "oMKxEAs5JLBcB5DmgGBDGgVoKAZ8KYZKD8Rw2A14qMEcLEwFgmgCBJCMwII8nAEw0KpwhgAAAP//"
                + "aOR/w4dgAh9IAAAAAElFTkSuQmCC";

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            System.out.println("Decoded successfully!");
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string: " + e.getMessage());
        }
    }
}

