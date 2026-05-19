
package com.batoilogic.api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Esta clase funciona como un "Demonio". En programación, un demonio es simplemente
 * un servicio que corre en segundo plano sin molestar al usuario, esperando a que
 * llegue una hora concreta para hacer una tarea.
 */
@Component
public class TareaProgramadaDaemon {

    /**
     * Este método se ejecuta solo gracias a la anotación @Scheduled.
     * El "cron" dice: Ejecútate a los 0 segundos, 0 minutos, de las 6 horas (6:00 AM).
     * El resto de signos "?" y "*" significan "todos los días".
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void ejecutarAutomatizacionDiaria() {
        // Un mensaje en la consola de Spring para saber que el demonio se ha despertado
        System.out.println("Daemon PSP: Iniciando servicio en segundo pla a las 6:00 AM...");

        // Guardamos la fecha de hoy para pasársela como argumento al otro proceso
        String fechaHoy = LocalDate.now().toString();

        try {
            // Buscamos dónde está instalado Java en el ordenador actual para poder usarlo
            String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            // Usamos ProcessBuilder para arrancar un programa externo.
            // Le pasamos los comandos como si los escribiéramos en la terminal de Windows o Linux:
            // 1. Ejecutable de java
            // 2. "-cp" y el Classpath (para que sepa dónde buscar nuestras clases del proyecto)
            // 3. La ruta completa de la clase con el 'main' que queremos ejecutar
            // 4. La fecha de hoy como argumento del main (Paso de parámetros entre procesos)
            ProcessBuilder pb = new ProcessBuilder(
                    javaHome,
                    "-cp",
                    System.getProperty("java.class.path"),
                    "com.batoilogic.api.service.ProcesoExportacionYAsignacion",
                    fechaHoy
            );

            // Redirigimos la salida del proceso hijo al proceso padre.
            // Gracias a esto, los System.out.print del proceso que lanzamos se verán
            // en la misma consola de Spring Boot.
            pb.inheritIO();

            System.out.println("ProcessBuilder: Lanzando subproceso independiente para la fecha: " + fechaHoy);

            // Al hacer ".start()" el proceso se ejecuta de forma asíncrona (va a su bola).
            // La API de Spring sigue funcionando y este método termina sin quedarse congelado esperando.
            Process proceso = pb.start();

        } catch (IOException e) {
            System.err.println("Error al lanzar el subproceso con ProcessBuilder: " + e.getMessage());
        }
    }
}