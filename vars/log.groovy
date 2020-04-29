def printRed(message) {
  println "\u001b[31m" + message + "\u001b[0m"
}

def printGreen(message) {
  println "\u001b[32;1m" + message + "\u001b[0m"
}

def printMagenta(message) {
  println "\u001b[35m" + message + "\u001b[0m"
}

def checkCurl(response) {
  if (response.contains('200')) {
    printMagenta("[INFO] Response code is 200")
  } else if (response.contains('302')) {
    printMagenta("[INFO] Response code is 302")
  } else if (response.contains('status')) {
    printMagenta("[INFO] Bundles successfully refreshed")
  } else if (response.contains('401')) {
    printRed("[ERROR] Unauthorized request")
    currentBuild.result = 'UNSTABLE'
  } else {
    printRed("[ERROR] Unable to send cURL request")
    currentBuild.result = 'UNSTABLE'
  }
}
