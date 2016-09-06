package org.bpmnwithactiviti.chapter5;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;

import java.io.FileInputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

public class BarDeployer {

    public static void main(String[] args) throws Exception {
        ProcessEngineConfiguration processEngineConfigurator = new StandaloneProcessEngineConfiguration();
        processEngineConfigurator.setDatabaseType("mysql");
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("root");
        dataSource.setServerName("localhost");
        dataSource.setPort(3306);
        dataSource.setDatabaseName("activiti");
        processEngineConfigurator.setDataSource(dataSource);
        processEngineConfigurator.setHistory("full");
        processEngineConfigurator.setDatabaseSchemaUpdate("true");
        processEngineConfigurator.setJobExecutorActivate(true);



		/*ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();*/
        ProcessEngine processEngine = processEngineConfigurator.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        /*String barFileName = "src/main/resources/chapter5/dist/loanrequest.bar";*/
        String barFileName = "src/main/resources/chapter5/dist/jiraissue.bar";
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(barFileName));
        String deploymentID = repositoryService.createDeployment().name(barFileName).addZipInputStream(inputStream).deploy().getId();
        List<String> deployedResources = repositoryService.getDeploymentResourceNames(deploymentID);
        for (String deployedResource : deployedResources) {
            System.out.println("Deployed : " + deployedResource);
        }
        inputStream.close();
    }
}
