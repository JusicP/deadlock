import java.util.ListIterator;
import java.util.Vector ;


public class DeadlockManager
{

  private static Vector<Resource> resources ;
  private static Vector<Process> processes ;

  public static void setResources( Vector<Resource> newResources )
  {
    resources = newResources ;
  }

  public static void setProcesses( Vector<Process> newProcesses )
  {
    processes = newProcesses ;
  }

  /**
  Can the process be granted the resource?
  */
  public static boolean grantable( int id , Resource resource )
  {
    return available( id , resource ) && isStateSafe(id, resource) ;
  }

  public static boolean available( int id , Resource resource )
  {
    return ( resource.getCurrentAvailable() > 0 ) ;
  }

  public static boolean isStateSafe(int id, Resource resource) {
    Process currentProcess = processes.get(id);


    // print resource table
    System.out.printf("%10s | %10s | %10s\n",
            "id",
            "has",
            "max"
    );

    for (Process process : processes) {
      if (process.state == Process.STATE_HALT) {
        continue;
      }

      if (process.getId() == currentProcess.getId()) {
        System.out.printf("%10d | %10d | %10d\n",
                process.getId(),
                process.getAllocatedResourceCount(resource) + 1,
                process.maxNumberOfAllocatedResources.get(resource.getId())
        );
      } else {
        System.out.printf("%10d | %10d | %10d\n",
                process.getId(),
                process.getAllocatedResourceCount(resource),
                process.maxNumberOfAllocatedResources.get(resource.getId())
        );
      }
    }

    int availableResourceCount = resource.getCurrentAvailable() - 1;
    System.out.printf("Free: %d\n", availableResourceCount);

    //Vector<Process> set = (Vector<Process>) processes.clone();
    //set.removeIf(process -> process.state == Process.STATE_HALT);

    boolean[] finish = new boolean[processes.size()];
    while (true) {
      boolean found = false;
      for (int i = 0; i < processes.size(); i++) {
        Process process = processes.get(i);
        int allocatedResourceCount = 0;
        int allocatedResourceMaxNumber = process.maxNumberOfAllocatedResources.get(resource.getId());
        if (process.getId() == currentProcess.getId()) {
          allocatedResourceCount = process.getAllocatedResourceCount(resource) + 1;
        } else {
          allocatedResourceCount = process.getAllocatedResourceCount(resource);
        }

        // якщо можна виділити ресурси, то симулюємо їх виділення та звільнення та помічаємо процес як завершений
        if (!finish[i] && (allocatedResourceMaxNumber - allocatedResourceCount <= availableResourceCount)) {
          availableResourceCount += allocatedResourceCount; // звільнюємо
          finish[i] = true;
          found = true;
        }
      }

      // всі процеси або можуть завершитись або не можуть
      if (!found) {
        break;
      }
    }

    // перевіряємо чи всі процеси можуть завершитись
    for (int i = 0; i < processes.size(); i++) {
      if (!finish[i]) {
        System.out.println("Can't allocate resource!") ;
        return false;
      }
    }

    return true;
  }

  public static void allocate( int id , Resource resource )
  {
    resource.setCurrentAvailable( resource.getCurrentAvailable() - 1 ) ;
    // we also need to note that the process has the resource allocated to it
    Process p = (Process)processes.elementAt(id);
    p.addAllocatedResource( resource ) ;
  }

  public static void deallocate( int id , Resource resource )
  {
    resource.setCurrentAvailable( resource.getCurrentAvailable() + 1 ) ;
    // we also need to note that this process no longer has the resource allocated
    Process p = (Process)processes.elementAt(id);
    p.removeAllocatedResource( resource ) ;
  }

  /**
  all processes are blocked.  One of them should be killed 
  and its resources deallocated so that the others can continue.
  */
  public static void deadlocked()
  {
  }

}