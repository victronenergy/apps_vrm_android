@Logout
Feature: Logout

  Scenario: As a user I would like to see preview of the app and be able to logout
    Given I am on the login screen
    When I login as a demo user
    Then I wait for the sitelist to appear
    Then I log out
    Then I should be on the login screen

  Scenario: As a valid user I can log into my app and logout
    Given I am on the login screen
    When I enter a valid email and password
    And I press the Login button
    Then I wait for the sitelist to appear
    Then I log out
    Then I should be on the login screen

  Scenario: As a demo user I would like to see demo on the logout button so I can see that I am logged in as a demo user
    Given I am on the login screen
    When I login as a demo user
    Then I wait for the sitelist to appear
    Then I click on the settings button
    Then I should see "Demo" on the logout button

  Scenario: As a user I would like to see my email on the logout button so I can see as which user I am logged in
    Given I am on the login screen
    When I enter a valid email and password
    And I press the Login button
    Then I wait for the sitelist to appear
    Then I click on the settings button
    Then I should see "kilian@m2mobi.com" on the logout button