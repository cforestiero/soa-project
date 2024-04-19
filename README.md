> Trabajo práctico de Sistemas Operativos Avanzados
# Smart Pool
## Descripción
Solución para automatizar y gestionar los procesos de una piscina, permitiendo a los usuarios controlar el ciclo de filtrado y las luces RGB de forma remota a través de una app. El sistema realizará únicamente filtrado por la toma de fondo, mientras que el usuario podrá realizar desagote manualmente con instrucciones proporcionadas a través de la app.

Para el desarrollo se utilizara la placa Arduino UNO.

### Sensores:
- Sensor de Temperatura del Agua: Monitorea la temperatura del agua de la piscina para ajustar los ciclos de filtrado según las condiciones climáticas.(esto pasa porque en verano si la pileta queda 1 dia tapada, tenes mucha agua caliente en la superficie que no se mezcla con la del fondo)
- Sensor de Nivel de Agua (Sensor de distancia por ultrasonido (HC-SR04)): Detecta el nivel de agua en la piscina y proporciona información sobre el estado del sistema de filtrado y bombeo.(Por si ocurre que el nivel por X motivo es bajo y al querer filtrar o desagotar entra aire a la bomba y la quema)
- Sensor de Luz Ambiental: Permite ajustar la intensidad y el color de las luces RGB(LED) de la piscina según las condiciones de iluminación ambiental.

### Actuadores:
- Bomba de Filtrado: Se activa para iniciar y detener los ciclos de filtrado a través de la toma de fondo, basándose en la programación configurada por el usuario a través de la aplicación móvil.(la idea es solo darle corriente con algún relé para que empiece el filtrado)
- Luces RGB(LED): Se pueden encender, apagar y ajustar remotamente a través de la aplicación móvil para personalizar la iluminación de la piscina.(sino usamos solo 3 LEDS a modo de prueba). Un modo noche, así justifica el sensor de luz ambiental.

## Consignas del trabajo

### Circuito Electrónico
#### Circuito completo:
 [Simulación del circuito en Thinkercad](http://handlebarsjs.com/)  _[En desarrollo]_
 #### Ejemplos de los sensores en thinkercad:
 TBD
 
 _[s1](link 1)_
 
 _[s2](link 2)_
 
 _[s3](link 3)_

### Desarrollo del SE
En este repositorio se encontraran los archivos que corresponden al código fuente del sistema embebido, siguiendo el patrón de diseño _(máquina de estados, a definir)_.

### Informe
[link al informe](https://docs.google.com/document/d/1_LNlBqL6A76YgOMo5v6x_5xJuPPDEOqsAeQZE0QdtuY/edit)
