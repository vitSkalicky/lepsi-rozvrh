# Branching model

Since 065e33a [Vincent Driessen's branching model](https://nvie.com/posts/a-successful-git-branching-model/) is being used. It works like this: ![branching model scheme](https://nvie.com/img/git-model@2x.png)

Development branch is called `dev` and all feature branches are called `feat/blahblah`. **!!! Do not base your work on any `feature` branch !!!** because the author may rebase it before merging (even if it is pushed).