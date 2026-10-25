# analog
CMPUT 301 Fall 2026 team repository for project development.

<br>

# **WORK IN PROGRESS ** 

## Software V0.0.1


This README will currently hold hyperlinked text to various parts of the project, small tutorials, or
how-to's.

### Quick Links

Click on the word to redirect to the location:

[code](./application/app/src/main/java/com/example/birchbark)  
[wiki](https://github.com/CMPUT301F26analog/analog-events/wiki)  
[backlog](https://github.com/orgs/CMPUT301F26analog/projects/2)  
[docs](./doc)  
[images](./doc)  
[references]()  
[llm]()  

### Using github and the 'Feature Branch Workflow'

This specifically ties to the developers of _BirchBark_ for _team analog_. We will utilize the process
and theory of _**feature branches**_. Please see the link [here](https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow)
for how this will work.

<br>
 
#### The TLDR:

1. Clone the repository first
2. Check for updates to main branch periodically with `git status`

Create your _**feature branch**_:  
3. `git switch main` Start on main first (good practice to check for main updates)  
4. `git pull --ff-only origin main` Pull from main only  
5. `git switch -c feature/$YOUR_FEATURE_NAME_HERE$` This creates a feature branch (whatever you are workign on, usually a `TODO` or `US.`  

Now any changes made to the branch goes to YOUR feature branch and NOT MAIN.

Commit the changes:  
6. `git status` See the current differences, what is or is not being tracked  
7. `git add .` or `git add -A` Stages all the changes for commit (can also specifically choose as well.)  
8. `git commit -m "Your commit message."` Please try to be descriptive, dont say "update stuff."  
9. `git push -u origin feature/$YOUR_FEATURE_NAME_HERE$` Note that -u is only required for the first push (upstream setup), after, just use `git push`  

<br>

### To create a pull request for main (Only do PRs between 11AM and 11PM please)

Quick how to for a pull request:

1. Open the repository on github, or click [here](https://github.com/CMPUT301F26analog/analog-events/pulls)  
2. Or Navitage to Pull requests -> New pull request, or select Compare & pull reqest from branch of notification  
3. Set the base branch to `main`  
4. Set the compare branch to `feature/$YOUR_FEATURE_NAME_HERE$`  
5. Explain what the change was, if its a big change, we should walk through it in our scrum/team meeting  
6. Have at minimum 1 person to review it--2 may be better  
7. After the approval, the PR should merge with main.  

This [link](https://docs.github.com/en/pull-requests/how-tos/create-pull-requests/creating-a-pull-request) has more information on pull requests.  

7. `git switch main` Update your local repository  
8. `git pull -ff-only origin main`  

**REMOVE YOUR FEATURE BRANCH ONCE IT HAS BEEN MERGED WITH MAIN**
9. `git branch -d feature/$YOUR_FEATURE_NAME_HERE$`  

<br>

### How to update your branch if you are working on a function and main was updated

#### If your changes are already commited
1. `git switch feature/$YOUR_FEATURE_NAME_HERE$` Make sure you are on your feature branch  
2. `git fetch origin` Get the latest remote changes from main  
3. `git merge origin/main`Merge the current main into your branch  

You may need to resolve configs.  

4. `git push`  

#### Your changes have not been commited

1. `git switch feature/$YOUR_FEATURE_NAME_HERE$` Make sure you are on your feature branch  
2. `git stash push -u -m "My feature in progress"` Temporarily save your local changes and untracked files  
3. `git fetch origin`  
4. `git merge origin/main`  
5. `git stash pop` Restores your local edits and file changes  


#### If MULTIPLE people are working on a feature

If you want to resynch with a recent push to the feature branch  
1. `git switch feature/$YOUR_FEATURE_NAME_HERE$`  
2. `git pull origin feature/$YOUR_FEATURE_NAME_HERE$`  

## IF ANYTHING BAD HAPPENS OR YOU GET MULTIPLE WARNINGS, STOP.
`git merge --abort`  
