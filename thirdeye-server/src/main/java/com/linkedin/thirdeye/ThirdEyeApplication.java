package com.linkedin.thirdeye;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.linkedin.thirdeye.api.StarTreeManager;
import com.linkedin.thirdeye.impl.StarTreeManagerImpl;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.concurrent.ExecutorService;

public class ThirdEyeApplication extends Application<ThirdEyeApplication.Config>
{
  public static final String NAME = "thirdeye";
  public static final String BETWEEN = "__BETWEEN__";
  public static final String IN = "__IN__";
  public static final String TIME_SEPARATOR  = ",";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public void initialize(Bootstrap<Config> thirdEyeConfigurationBootstrap)
  {
    // Do nothing
  }

  @Override
  public void run(Config thirdEyeConfiguration, Environment environment) throws Exception
  {
    ExecutorService executorService
            = environment.lifecycle()
                         .executorService("starTreeManager")
                         .minThreads(Runtime.getRuntime().availableProcessors())
                         .maxThreads(Runtime.getRuntime().availableProcessors())
                         .build();

    final StarTreeManager starTreeManager = new StarTreeManagerImpl(executorService);

    final ThirdEyeMetricsResource metricsResource = new ThirdEyeMetricsResource(starTreeManager);
    final ThirdEyeBootstrapResource bootstrapResource = new ThirdEyeBootstrapResource(starTreeManager);
    final ThirdEyeConfigResource configResource = new ThirdEyeConfigResource(starTreeManager);
    final ThirdEyeDimensionsResource dimensionsResource = new ThirdEyeDimensionsResource(starTreeManager);
    final ThirdEyeCollectionsResource collectionsResource = new ThirdEyeCollectionsResource(starTreeManager);

    final ThirdEyeHealthCheck healthCheck = new ThirdEyeHealthCheck();

    environment.healthChecks().register(NAME, healthCheck);

    environment.jersey().register(metricsResource);
    environment.jersey().register(bootstrapResource);
    environment.jersey().register(configResource);
    environment.jersey().register(dimensionsResource);
    environment.jersey().register(collectionsResource);
  }

  public static class Config extends Configuration
  {
    @NotEmpty
    private String dataRoot;

    @JsonProperty
    public String getDataRoot()
    {
      return dataRoot;
    }

    @JsonProperty
    public void setDataRoot(String dataRoot)
    {
      this.dataRoot = dataRoot;
    }
  }

  public static void main(String[] args) throws Exception
  {
    new ThirdEyeApplication().run(args);
  }
}
