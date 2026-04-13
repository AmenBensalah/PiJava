package edu.esportify.controllers;

public class UserFeedController extends FeedController implements UserContentController {
    @Override
    public void init(UserLayoutController parentController) {
        super.init(null);
    }
}
