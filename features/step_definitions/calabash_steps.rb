require 'calabash-android/calabash_steps'

When /^I enter an invalid email$/ do
	steps %{
		Then I enter "no_username" into input field number 1
	}
end

When /^I enter a valid email and no password$/ do
	steps %{
		Then I enter "kilian@m2mobi.com" into input field number 1
		Then I enter "" into input field number 2
	}
end

When /^I enter an invalid email and password$/ do
    steps %{
        Then I enter "killian@m2mobi.com" into input field number 1
        Then I enter "no_password" into input field number 2
    }
end

When /^I enter a valid email and password$/ do
    steps %{
        Then I enter "kilian@m2mobi.com" into input field number 1
        Then I enter "victron" into input field number 2
    }
end

When /^I login as a demo user$/ do
	steps %{
		Then I press the "Demo" button
	}
end

And /^I press the Login button$/ do
    steps %{
        Then I press the "Login" button
    }
end

Then /^an error message should appear stating "([^\"]*)"$/ do |name|
    steps %{
		Then I should see text containing "#{name}"
    }
end

Then /^I log out$/ do
	steps %{
		Then I press view with id "menu_item_site_list"
		Then I touch the "Logout" text
	}
end

Then /^I click on the settings button$/ do
	steps %{
		Then I press view with id "menu_item_site_list"
	}
end

Then /^I should see "([^\"]*)" on the logout button$/ do |name|
	steps %{
		Then I should see text containing "Logout - #{name}"
	}
end

Then /^I wait for the sitelist to appear$/ do
	steps %{
		And I wait for the view with id "list_sites" to appear
	}
end

Given /^I am on the login screen$/ do
	steps %{
		Then I wait for the "ActivityLogin" screen to appear
	}
end

Then /^I should be on the login screen$/ do
	steps %{
		Then I wait for the "ActivityLogin" screen to appear
	}
end