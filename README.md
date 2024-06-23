# Smart Pool

## Universidad Nacional de La Matanza

Departamento de Ingeniería e Investigaciones Tecnológicas

## Integrantes del Proyecto

- Rocío Belén Fernández
- Camila Julieta Forestiero
- Franco Ariel Kowalski
- Facundo Toloza

## Descripción del Proyecto

El proyecto **Smart Pool** consiste en la creación de un sistema automatizado para el monitoreo y control de una piscina. Utilizando sensores de temperatura, nivel de agua y luminosidad, junto con actuadores como bombas y luces LED, el sistema asegura una gestión eficiente y segura de la pileta. La gestión remota se realiza a través de una aplicación móvil Android, mientras que el desarrollo del sistema embebido utilizará la placa Arduino UNO.

### Características Principales

- **Monitoreo en tiempo real**: Utiliza sensores para medir la temperatura del agua, el nivel del agua y la luminosidad.
- **Control remoto**: Actuadores como bombas y luces LED RGB controlables mediante una aplicación Android.
- **Automatización**: Ciclos de filtrado y drenaje automatizados basados en las configuraciones del usuario.
- **Interfaz de usuario intuitiva**: Aplicación móvil fácil de usar que proporciona acceso a todas las funcionalidades del sistema.

## Sistema Embebido

### Sensores:

- Sensor de Temperatura del Agua: Monitorea la temperatura del agua de la piscina para ajustar los ciclos de filtrado según las condiciones climáticas.(esto pasa porque en verano si la pileta queda 1 dia tapada, tenes mucha agua caliente en la superficie que no se mezcla con la del fondo)
- Sensor de Nivel de Agua (Sensor de distancia por ultrasonido (HC-SR04)): Detecta el nivel de agua en la piscina y proporciona información sobre el estado del sistema de filtrado y bombeo.(Por si ocurre que el nivel por X motivo es bajo y al querer filtrar o desagotar entra aire a la bomba y la quema)
- Sensor de Luz Ambiental: Permite ajustar la intensidad y el color de las luces RGB(LED) de la piscina según las condiciones de iluminación ambiental.
- Interruptor Switch (Slide): Permite establecer el modo de la bomba de agua, alternando entre el modo de filtrado o drenaje.

### Actuadores:

- Bomba de Filtrado: Se activa para iniciar y detener los ciclos de filtrado a través de la toma de fondo, basándose en la programación configurada por el usuario a través de la aplicación móvil.(la idea es solo darle corriente con algún relé para que empiece el filtrado)
- Luces RGB(LED): Se pueden encender, apagar y ajustar remotamente a través de la aplicación móvil para personalizar la iluminación de la piscina.(sino usamos solo 3 LEDS a modo de prueba). Un modo noche, así justifica el sensor de luz ambiental.

## Aplicación Android

### Diagrama de Navegación

![Diagrama de Navegación](Diagrama_Navegacion.jpg)

### Activities

- WelcomeActivity: Pantalla de inicio, que permite iniciar con la gestión remota de la piscina.
- MainActivity: Su vista dependerá de la situación actual del sistema embebido. Si el modo se encuentra en “Desagote” (dependiente del switch físico encontrado en el sistema embebido) sin haber iniciado aún, se podrá visualizar un botón que dirigirá a una pantalla relacionada al inicio del Desagote. En cambio, si el modo es “Filtrado”, dicho botón no será visible, solamente pudiendo acceder a la configuración (la tuerca situada en la esquina superior derecha), estadísticas y configuración de las luces LED.
- FilterConfigurationActivity: Se permite la personalización del ciclo de filtrado, pudiendo establecer cada cuántas horas deseará que se inicie de forma automática el filtrado del agua. Esta automatización solamente será funcional cuando el usuario establezca el modo Filtrado a través del switch físico del sistema embebido, teniendo en cuenta que la manguera de la bomba de agua deberá encontrarse situada para tal fin (dentro de la piscina).
- StatsActivity: Visualización de la información más relevante de la situación actual de la piscina, ya sea la temperatura del agua, la distancia del agua respecto al sensor de distancia situado en el borde de la piscina, además de la fecha y hora tanto de la última vez que se realizó el filtrado como de la última vez que se realizó el desagote.
- LightsActivity: Se tendrá a disposición un switch bajo el nombre “Luces”, que permitirá forzar el encendido o apagado de las luces para adaptarse al deseo del usuario. En caso de tener las luces encendidas, el usuario podrá establecer el color de las mismas gracias al acelerómetro del celular. Por lo tanto, bastará con mover el celular respecto a sus distintos ejes para elegir el color deseado, modificando los componentes rojo, verde y azul del color mediante el movimiento del dispositivo respecto a los ejes X, Y y Z respectivamente. Una vez de que el color sea del agrado del usuario, podrá confirmarlo mediante el botón de “Confirmar” y será establecido en las luces LED de la piscina.
- DewaterActivity: Pantalla de confirmación del desagote de la piscina. Debe asegurarse de posicionar correctamente la manguera conectada a la bomba de agua (fuera de la piscina), ya que dicha confirmación implicará el encendido de la bomba de agua, la cual expulsará el agua a través de dicha manguera.

## Enlaces de Acceso

[Informe TP1](TP1_Lunes_3.pdf)

[Informe TP2](TP2_Lunes_3.pdf)

[Proyecto en Tinkercad](https://www.tinkercad.com/things/5F3eic6dEa0-smart-pool?sharecode=aoe_hmcSV0sjEGT4X_fzpzt_i47zsqm5JbVrOVHj2Xw)