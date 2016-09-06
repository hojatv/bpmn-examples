package org.bpmnwithactiviti.chapter7.javaexception;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
/*TODO : When you want to implement decision logic in a Java service task, the ActivityBehavior
  interface must be implemented instead of the JavaDelegate interface.
  Using the ActivityBehavior interface also means that you must explicitly implement logic to leave the service task.
  With the JavaDelegate interface,this isn’t necessary because it will take care of leaving the service task when the execute
  method is completed.
  */
public class OrderPersistTask implements ActivityBehavior {
  
  @PersistenceContext
  EntityManager entityManager;

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    PvmTransition transition = null;
    try {
      Order order = (Order) execution.getVariable("order");
      entityManager.persist(order);
      transition = execution.getActivity().findOutgoingTransition("orderPersisted");
    } catch(Throwable e) {
      transition = execution.getActivity().findOutgoingTransition("orderNotPersisted");
    }
    execution.take(transition);
  }
}
