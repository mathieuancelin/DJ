#!/bin/bash

[ -z "$DJ_URL" ] && DJ_URL='http://localhost:9000'

function play() 
{
	curl -X POST -o /dev/null $DJ_URL/commands/play -s
	echo "Playing the queue on $DJ_URL/commands/play"
	return;
}

function stop() 
{
	curl -X POST -o /dev/null $DJ_URL/commands/stop -s
	echo "Stopping the queue on $DJ_URL/commands/stop"
	return;
}

function next() 
{
	curl -X POST -o /dev/null $DJ_URL/commands/next -s
	echo "Nexting the queue on $DJ_URL/commands/next"
	return;
}

function clear() 
{
	curl -X POST -o /dev/null $DJ_URL/commands/clear -s
	echo "Clearing the queue on $DJ_URL/commands/clear"
	return;
}

function echoStart()
{
	echo " "
	echo "|==========================================|"
	echo "|                                          |"
	echo "|             Welcome in DJ !!!            |"
	echo "|                                          |"
	echo "|==========================================|"
	echo " "
	return;
}

function displayHelp()
{
	echoStart
    echo " "
    echo "You're targetting DJ server @ $APP_PATH "
    echo " "
	echo "You can use the following commands :"
	echo " "
	echo "  play        play the first song in the queue"
	echo "  stop        stop the current song"
	echo "  next        skip to next song"
	echo "  clear       clear the current queue"
	echo "  --help      display help"
	echo " "
	echo "Have fun :)"
	echo " "
	return;
}

case $1 in
 play)
  echoStart
  play $2
  ;;
 stop)
  echoStart
  stop $2
  ;;
 next)
  echoStart
  next $2
  ;;
 clear)
  echoStart
  clear $2
  ;;
 --help)
  displayHelp
  ;;
 *)
  #echo "display the help :)"
  ;;
esac

#complete -F _completion -o filenames dj
