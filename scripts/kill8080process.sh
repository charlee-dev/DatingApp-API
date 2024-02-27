#! /bin/zsh
# kill local server
kill -9 $(lsof -t -i:8080)
