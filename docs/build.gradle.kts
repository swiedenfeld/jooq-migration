plugins {
    id("org.antora") version "1.0.0"
}

node {
    version = "16.13.0"
}
antora {
    playbook.set(file("antora-playbook.yml"))
}
