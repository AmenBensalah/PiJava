package edu.esportify.controllers;

import javafx.fxml.FXML;

public class UserOrdersController implements UserContentController {
    private UserLayoutController parentController;

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void onStore() {
        if (parentController != null) {
            parentController.showStore();
        }
    }
}
