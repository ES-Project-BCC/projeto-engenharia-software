package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserControllerTest {
    UserController userController = new UserController();

    @Test
    void testMe() {
        UserController.MeResponse result = userController.me(new UserDetailsImpl(new User(Long.valueOf(1), "nome", "email", "password", Role.ADMIN)));
        Assertions.assertEquals(new UserController.MeResponse(1L, "nome", "email", Role.ADMIN), result);

    }

    @Test
    void testAdminPing() {
        String result = userController.adminPing();
        Assertions.assertEquals("pong - voce e ADMIN", result);

    }
}
