import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector ;

public class Process
{
  public static final int STATE_UNKNOWN = 0 ;
  public static final int STATE_COMPUTABLE = 1 ;
  public static final int STATE_RESOURCE_WAIT = 2 ;
  public static final int STATE_HALT = 3 ;

  protected int id ;
  private String filename = "" ;
  protected CommandParser cp ;
  protected int state = STATE_UNKNOWN ;
  protected int timeToCompute ;
  protected Resource resourceAwaiting = null ;
  private Vector<Resource> allocatedResources = new Vector<Resource>() ;
  protected Map<Integer, Integer> maxNumberOfAllocatedResources;

  public Process( int newId , String newFilename )
  {
    super() ;
    id = newId ;
    filename = newFilename ;
  }

  public int getId()
  {
    return id ;
  }

  public String getFilename()
  {
    return filename ;
  }

  public void setFilename(String newFilename)
  {
    filename = newFilename ;
  }

  public String getState()
  {
    switch( state )
    {
      case STATE_RESOURCE_WAIT:
        return "W" ;
      case STATE_COMPUTABLE:
        return "C" ;
      case STATE_HALT:
        return "H" ;
      default:
        return "U";
    }
  }

  public void setState( String newState )
  {
  if ( newState.equals("W") )
    state = STATE_RESOURCE_WAIT ;
  else if ( newState.equals("C") )
    state = STATE_COMPUTABLE ;
  else if ( newState.equals("H") )
    state = STATE_HALT ;
  else
    state = STATE_UNKNOWN ;
  }

  public Resource getResourceAwaiting( )
  {
    return resourceAwaiting ;
  }

  public void addAllocatedResource( Resource resource )
  {
    allocatedResources.addElement( resource ) ;
  }

  public void removeAllocatedResource( Resource resource )
  {
    allocatedResources.removeElement( resource ) ;
  }

  public Vector<Resource> getAllocatedResources( )
  {
    return allocatedResources;
  }

  public int getAllocatedResourceCount( Resource resource ) {
    int counter = 0;
    for (Resource res : allocatedResources) {
      if (res.getId() == resource.getId()) {
        counter++;
      }
    }
    return counter;
  }

  public void calcMaxNumberOfAllocatedResources() {
    maxNumberOfAllocatedResources = new HashMap<Integer, Integer>();
    Map<Integer, Integer> currentHeldResources = new HashMap<Integer, Integer>();

    while (true) {
      Command cmd = cp.getCommand();
      if (cmd == null) {
        // done
        break;
      }

      String keyword = cmd.getKeyword();
      if ( keyword.equals( "R" ) ) {
        int resourceId = cmd.getParameter();

        // update current number of allocated resources
        Integer count = currentHeldResources.get(resourceId);
        if (count == null) {
          // put new pair
          count = 1;
          currentHeldResources.put(resourceId, count);
        } else {
          // increment current resource
          count++;
          currentHeldResources.put(resourceId, count);
        }

        // update maximum number
        Integer maxCount = maxNumberOfAllocatedResources.get(resourceId);
        if (maxCount == null || count > maxCount) {
          maxNumberOfAllocatedResources.put(resourceId, count);
        }
      } else if ( keyword.equals( "F" ) ) {
        int resourceId = cmd.getParameter();
        Integer count = currentHeldResources.get(resourceId);
        if (count == null) {
          throw new RuntimeException("There is an error in the code of process " + filename + ", R" + resourceId + " is never requested but freed");
        } else {
          if (count - 1 < 0) {
            throw new RuntimeException("There is an error in the code of process " + filename + ", R" + resourceId + " is never freed");
          }

          // decrement current resource
          currentHeldResources.put(resourceId, count - 1);
        }
      }
    }
  }

  public void reset() throws IOException 
  {
    cp = new CommandParser( new BufferedInputStream( new FileInputStream( filename ) ) ) ;
    calcMaxNumberOfAllocatedResources();
    cp = new CommandParser( new BufferedInputStream( new FileInputStream( filename ) ) ) ;

    state = STATE_UNKNOWN ;
    timeToCompute = 0 ;
    resourceAwaiting = null ;
    allocatedResources.removeAllElements() ;
  }

}