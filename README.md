# ptsd-aware
 an app that helps Veterans alleviate PTSD symtoms


###Git workflow

1.) Start from branch "master". To check you are on master run `git branch`
  If you aren't on master run `git checkout origin master`

2.) Fetch the latest changes with `git fetch`

3.) Pull the latest changes with `git pull origin master`

4.) Create a new branch to do work on `git checkout -b YourBranchNameHere`

5.) Edit the files you want to change. View your changes with `git status` and `git diff`

6.) Add your changes `git add pathtofile` or `git add -u` (which adds all changes except for newly created files which must be explicitly added with `git add pathtotfile`)

7.) Once you have a good amount of changes added run `git commit -m "some commit messsage"` try to group changes into commits that make sense, for exampele a good commit message could be "Added the controller for the immersion techniques view and linked it to the correct data model"

8.) Push your changes to the remote branch via `git push origin YourBranchNameHere`

9.) Open https://github.com/alicew02/ptsd-aware/ in your browser and click the button that says "Create Pull Request Now"

10.) Get the code reviewed and then click the "Merge branch now" button

11.) Repeat from step 1 when theres a new feature you want to add
