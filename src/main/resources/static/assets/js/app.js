/*
 * Melhorias progressivas — tudo funciona sem JavaScript.
 * Carregado como arquivo próprio (a CSP do sistema bloqueia scripts inline).
 */
(function () {
    'use strict';
    document.documentElement.classList.add('js');

    // Envia automaticamente formulários marcados (seletor de tema, troca de perfil)
    document.querySelectorAll('form[data-auto-enviar] select').forEach(function (select) {
        select.addEventListener('change', function () { select.form.submit(); });
    });

    // Mostrar/ocultar senha
    document.querySelectorAll('[data-alternar-senha]').forEach(function (botao) {
        botao.addEventListener('click', function () {
            var campo = document.getElementById(botao.getAttribute('aria-controls'));
            if (!campo) return;
            var mostrar = campo.type === 'password';
            campo.type = mostrar ? 'text' : 'password';
            botao.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
        });
    });

    // Checklist da política de senha em tempo real (a validação real é no servidor)
    document.querySelectorAll('[data-requisitos-de]').forEach(function (lista) {
        var campo = document.getElementById(lista.getAttribute('data-requisitos-de'));
        if (!campo) return;
        var regras = {
            tamanho: function (s) { return s.length >= 10 && s.length <= 64; },
            maiuscula: function (s) { return /\p{Lu}/u.test(s); },
            minuscula: function (s) { return /\p{Ll}/u.test(s); },
            numero: function (s) { return /\d/.test(s); },
            simbolo: function (s) { return /[^\p{L}\d\s]/u.test(s); }
        };
        campo.addEventListener('input', function () {
            lista.querySelectorAll('[data-regra]').forEach(function (item) {
                var regra = regras[item.getAttribute('data-regra')];
                item.classList.toggle('ok', !!regra && regra(campo.value));
            });
        });
    });

    // Confirmação antes de ações destrutivas
    document.querySelectorAll('.botao--perigo').forEach(function (botao) {
        botao.addEventListener('click', function (e) {
            if (!window.confirm('Confirma esta ação?')) e.preventDefault();
        });
    });
})();
