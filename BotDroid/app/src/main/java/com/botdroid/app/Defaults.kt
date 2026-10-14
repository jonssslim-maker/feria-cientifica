package com.botdroid.app

object Defaults {
    private fun mk(id: String, name: String, kw: String, greeting: Boolean, enabled: Boolean,
                   a: String, n: String, p: String, c: String) =
        Rule(id, name, kw, greeting, enabled,
            mutableMapOf("amigable" to a, "natural" to n, "profesional" to p, "comercial" to c))

    fun rules(): MutableList<Rule> = mutableListOf(
        mk("envios", "Envíos", "envi, mandan, delivery, a domicilio", false, true,
            "¡Siii! 📦 Hacemos envíos a nivel nacional. ¿De qué ciudad nos escribes? 😊\nSí, enviamos a todo el país 📦 ¿A qué ciudad sería?",
            "Sí, hacemos envíos a todo el país. ¿A qué ciudad?\nSí enviamos, ¿a qué ciudad sería?",
            "Realizamos envíos a nivel nacional. ¿Podría indicarnos su ciudad?",
            "Hacemos envíos a nivel nacional 📦 Dime tu ciudad y coordinamos tu pedido."),
        mk("ubicacion", "Ubicación", "donde estan, ubicad, ubicacion, direccion, donde queda, de donde son, tienda fisica", false, true,
            "Somos de La Paz, Bolivia 🇧🇴, pero hacemos envíos a nivel nacional. 🤗\nEstamos en La Paz, Bolivia 🇧🇴 y enviamos a todo el país 😊",
            "Somos de La Paz, Bolivia, y enviamos a todo el país.",
            "Nos encontramos en La Paz, Bolivia. Realizamos envíos a nivel nacional.",
            "Estamos en La Paz, Bolivia 🇧🇴 Enviamos a todo el país, ¿de qué ciudad nos escribes?"),
        mk("pagos", "Pagos (DESACTIVADA: configura tus opciones de pago antes de activarla)", "pagar, pago, forma de pago, metodos de pago, qr, transferencia, deposito", false, false,
            "¡Claro! 😊 Te paso las opciones de pago disponibles para que puedas realizar tu compra.",
            "Claro, te paso las opciones de pago.",
            "Con gusto le compartimos las opciones de pago disponibles.",
            "¡Claro! Te paso las opciones de pago para que puedas hacer tu compra 😊"),
        mk("compra", "Quiero comprar", "comprar, me interesa, quiero uno, quiero el reloj, como hago, como lo pido, pedir", false, true,
            "¡Genial! 😊 Para coordinar tu pedido, ¿de qué ciudad nos escribes?\n¡Qué bueno! 🙌 Dime tu ciudad y vemos cómo coordinamos tu compra.",
            "Genial. ¿De qué ciudad nos escribes para coordinar tu pedido?",
            "Con gusto le ayudamos con su pedido. ¿Podría indicarnos su ciudad?",
            "¡Genial! Para coordinar tu pedido necesito saber de qué ciudad nos escribes 😊"),
        mk("precio", "Precio", "precio, cuanto, costo, valor, a como, vale, sale", false, true,
            "El reloj está a 290 Bs 🤩 Viene con su manilla, cadena, regulador y cajita 🎁\nTe cuento: son 290 Bs y viene con manilla, cadena, regulador y cajita 🎁",
            "El reloj está a 290 Bs. Incluye manilla, cadena, regulador y cajita.",
            "El reloj tiene un precio de 290 Bs e incluye manilla, cadena, regulador y caja.",
            "El reloj está a 290 Bs y viene completo: manilla, cadena, regulador y cajita 🎁 ¿Te lo enviamos?"),
        mk("saludo", "Saludo", "hola, ola, buenas, buen dia, buenos dias, hey, que tal", true, true,
            "¡Holaaa! 🤗✨ ¿Cómo estás?\n¡Holaa! 😊 Qué gusto saludarte, ¿en qué te ayudo?",
            "Hola 🙂 ¿cómo estás?\nHola, dime nomás.",
            "Buenas, ¿en qué podemos ayudarle?",
            "¡Hola! 😊 ¿Buscas algún reloj? Cuéntame y te ayudo.")
    )
}
