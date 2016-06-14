# javaagent-exit

A Java Agent that shuts down after installed. The agent should be installed
dynamically with Sun Attach API. Here is an example, how:

    VirtualMachine vm = VirtualMachine.attach(pid);
    vm.loadAgent(pathToShutdownAgentJar, args);

Three arguments are supported:

 - __exitcode:__ The exit code that the agent should use to stop the JVM.
   The code is used when everything went well; all shutdown hooks ran and
   there are only daemon threads left. The default value is _0_.
 - __timeout:__ The agent waits until the timeout, before shuts down the JVM
   forcibly by calling _Runtime.halt(haltcode)_. The value is specified in
   milliseconds. If the value is less than or equal to _0_, the agent will
   wait forever for a clean shutdown. The default value is _0_.
 - __haltcode:__ The code that the agent uses during stopping the JVM forcibly
   after the _timeout_. Default value is _1_.

Arguments should be separated by comma. E.g.:

    vm.loadAgent(pathToShutdownAgentJar, "timeout=1000,haltcode=5");
