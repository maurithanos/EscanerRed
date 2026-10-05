# EscanerRed
Un escáner que va a ver todos los dispositivos en la red y va a devolver la IP, el ping de cada uno, el nombre y si esta activo o no.

Configurar los parámetros de escaneo:   IP de inicio y IP de fin: Ingresa el rango de direcciones IP que deseas analizar (por ejemplo, de 10.160.7.223 a 10.160.7.233).   Tiempo de espera (ms): Define el tiempo límite de respuesta asignado a cada IP (por defecto 1000 ms).   Número de reintentos: Ajusta cuántas veces volverá a probar si una dirección IP no responde. 

Iniciar el escaneo:   Haz clic en el botón Iniciar escaneo en la esquina inferior izquierda.   El programa comenzará a comprobar cada dirección dentro del rango y mostrará los resultados en la tabla con las columnas: IP, Nombre equipo, Activo y Tiempo (ms). 

Detener o filtrar la búsqueda (opcional):   Puedes presionar Detener escaneo si deseas cancelar el proceso antes de que finalice la barra de progreso.   Si deseas ver únicamente las IP en línea, haz clic en el botón Mostrar solo activos. 

Guardar los resultados:   Una vez completado el escaneo, haz clic en Guardar resultados.   Se abrirá la ventana del explorador para elegir la ubicación de destino (por ejemplo, Descargas) y asignar un nombre al archivo (por defecto con extensión .csv).   Haz clic en Guardar para confirmar. 

Abrir y consultar el reporte exportado:   Ve a la carpeta donde guardaste el archivo .csv y ábrelo con un editor de texto (como el Bloc de notas) o una hoja de cálculo.   Allí verás detallada la lista de cada IP escaneada junto a su estado de actividad y tiempo de respuesta.   
