package com.redes.escaner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EscanerRedService {

    private static final String IP_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
          + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern PATTERN = Pattern.compile(IP_REGEX);

    public EscanerRedService() {
    }

    /**
     * Comprueba si una cadena es una dirección IPv4 válida.
     */
    public static boolean esIpValida(String ip) {
        return ip != null && PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Convierte una IPv4 a un número long.
     *
     * Ejemplo:
     * 192.168.1.1 -> 3232235777
     */
    public static long ipToLong(String ipAddress) {
        String[] ipNums = ipAddress.split("\\.");

        long result = 0;

        for (int i = 0; i < ipNums.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipNums[i]);

            result += (long) (ip * Math.pow(256, power));
        }

        return result;
    }

    /**
     * Convierte un número long a IPv4.
     */
    public static String longToIp(long i) {
        long a = (i >> 24) & 255;
        long b = (i >> 16) & 255;
        long c = (i >> 8) & 255;
        long d = i & 255;

        return a + "." + b + "." + c + "." + d;
    }

    /**
     * Realiza un ping utilizando el comando del sistema operativo.
     */
    public static ResultadoPing ejecutarPingSistema(String ip, int timeoutMs) {

        long inicioRelojJava = System.currentTimeMillis();

        try {
            boolean esWindows = System
                    .getProperty("os.name")
                    .toLowerCase()
                    .contains("win");

            ProcessBuilder builder;

            if (esWindows) {

                builder = new ProcessBuilder(
                        "ping",
                        "-n",
                        "1",
                        "-w",
                        String.valueOf(timeoutMs),
                        ip
                );

            } else {

                // Linux / Unix / macOS
                int timeoutSeg = Math.max(1, timeoutMs / 1000);

                builder = new ProcessBuilder(
                        "ping",
                        "-c",
                        "1",
                        "-W",
                        String.valueOf(timeoutSeg),
                        ip
                );
            }

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            boolean respondio = false;
            long msExtraidosConsola = -1;

            String line;

            while ((line = reader.readLine()) != null) {

                String lineLower = line.toLowerCase();

                /*
                 * Windows normalmente contiene:
                 *
                 * TTL=128
                 *
                 * Linux:
                 *
                 * bytes from ... ttl=64
                 */
                if (!lineLower.contains("ttl=")
                        && !lineLower.contains("bytes=")) {
                    continue;
                }

                if (lineLower.contains("unreachable")
                        || lineLower.contains("inaccesible")
                        || lineLower.contains("agotado")) {
                    continue;
                }

                respondio = true;

                /*
                 * Intenta obtener el tiempo mostrado por ping.
                 *
                 * Ejemplos:
                 * time=2 ms
                 * tiempo=2ms
                 * time<1ms
                 */
                Pattern tiempoPattern = Pattern.compile(
                        "(tiempo|time)\\s*[:=<]?\\s*(\\d+)"
                );

                Matcher matcher = tiempoPattern.matcher(lineLower);

                if (matcher.find()) {

                    msExtraidosConsola =
                            Long.parseLong(matcher.group(2));

                } else if (lineLower.contains("<1ms")
                        || lineLower.contains("<1 ms")
                        || lineLower.contains("< 1ms")
                        || lineLower.contains("< 1 ms")
                        || lineLower.contains("<1m")) {

                    msExtraidosConsola = 1;
                }
            }

            int exitCode = process.waitFor();

            long finRelojJava = System.currentTimeMillis();

            if (exitCode == 0 && respondio) {

                long duracionJava =
                        finRelojJava - inicioRelojJava;

                long tiempoFinal;

                if (msExtraidosConsola >= 0) {
                    tiempoFinal = msExtraidosConsola;
                } else {
                    tiempoFinal = duracionJava;
                }

                if (tiempoFinal <= 0) {
                    tiempoFinal = 1;
                }

                return new ResultadoPing(true, tiempoFinal);
            }

        } catch (Exception e) {
            // El equipo no responde o el comando ping no está disponible.
        }

        return new ResultadoPing(false, 0);
    }

    /**
     * Método compatible con EscanerRedFrame.
     *
     * Devuelve true si la IP responde al ping.
     */
    public static boolean hacerPing(String ip, int timeoutMs) {
        ResultadoPing resultado =
                ejecutarPingSistema(ip, timeoutMs);

        return resultado.exito;
    }

    /**
     * Ejecuta nslookup para obtener el nombre del equipo.
     */
    public static String ejecutarNslookupSistema(String ip) {

        try {

            ProcessBuilder builder = new ProcessBuilder(
                    "nslookup",
                    ip
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;

            while ((line = reader.readLine()) != null) {

                String lower = line.toLowerCase();

                if (lower.contains("name:")
                        || lower.contains("nombre:")) {

                    String[] partes = line.split(":", 2);

                    if (partes.length > 1) {
                        return partes[1].trim();
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            // Ignorar y devolver Desconocido.
        }

        return "Desconocido";
    }

    /**
     * Método compatible con EscanerRedFrame.
     */
    public static String obtenerNombreEquipo(String ip) {
        return ejecutarNslookupSistema(ip);
    }

    /**
     * Escanea una IP.
     */
    public static Dispositivo escanearIp(
            String ipStr,
            int timeoutMs,
            int reintentos) {

        long inicio = System.currentTimeMillis();

        boolean activo = false;
        String nombre = "Desconocido";

        int intentosTotales = reintentos + 1;

        for (int r = 0; r < intentosTotales; r++) {

            ResultadoPing resultado =
                    ejecutarPingSistema(ipStr, timeoutMs);

            if (resultado.exito) {
                activo = true;
                break;
            }
        }

        if (activo) {
            nombre = ejecutarNslookupSistema(ipStr);
        }

        long fin = System.currentTimeMillis();

        long tiempoTotal = fin - inicio;

        System.out.println(
                "IP: " + ipStr
                + " | Activa: " + activo
                + " | Tiempo: " + tiempoTotal + " ms"
        );

        return new Dispositivo(
                ipStr,
                nombre,
                activo,
                tiempoTotal
        );
    }

    /**
     * Resultado interno del ping.
     */
    public static class ResultadoPing {

        public final boolean exito;
        public final long tiempoMs;

        public ResultadoPing(
                boolean exito,
                long tiempoMs) {

            this.exito = exito;
            this.tiempoMs = tiempoMs;
        }
    }
}
