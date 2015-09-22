@Login
Feature: Login

  Scenario: When I don't type a valid email address I should see an error
    Given I am on the login screen
    When I enter an invalid email
    And I press the Login button
    Then an error message should appear stating "Please enter a valid email address."

  Scenario: When I don't type a password
    Given I am on the login screen
    When I enter a valid email and no password
    And I press the Login button
    Then an error message should appear stating "Please enter a password."

  Scenario: As a user I would like to see an error message when my email/password is incorrect
    Given I am on the login screen
    When I enter an invalid email and password
    And I press the Login button
    Then an error message should appear stating "Wrong email or password"

  Scenario: As a demo user I would like to preview the app
    Given I am on the login screen
    When I login as a demo user
    Then I wait for the sitelist to appear
    Then I wait to see "Live demo - Hub 1"
    Then I log out

  Scenario: As a valid user I can log into my app
    Given I am on the login screen
    When I enter a valid email and password
    And I press the Login button
    Then I wait for the sitelist to appear
    Then I wait to see "jacques"

  Scenario: As a valid user that was already logged in I should automatically login
    Then I wait for the sitelist to appear
    Then I wait to see "jacques"
    Then I log out

  Scenario: As a valid user that's auto logged in I want the app to not auto login when I logged out
    Given I am on the login screen