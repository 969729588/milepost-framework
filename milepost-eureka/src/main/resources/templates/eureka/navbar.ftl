<h1>系统状态</h1>
<div class="row">
  <div class="col-md-6">
    <table id='instances' class="table table-condensed table-striped table-hover">
      <#if amazonInfo??>
        <tr>
          <td>EUREKA SERVER</td>
          <td>AMI: ${amiId!}</td>
        </tr>
        <tr>
          <td>Zone</td>
          <td>${availabilityZone!}</td>
        </tr>
        <tr>
          <td>instance-id</td>
          <td>${instanceId!}</td>
        </tr>
      </#if>
      <tr>
        <td>环境</td><!--默认是test，可以通过eureka.environment参数设置，暂时没用上。-->
        <td>${environment!}</td>
      </tr>
      <tr>
        <td>数据中心</td><!--默认是default，可以通过eureka.datacenter参数设置，暂时没用上。-->
        <td>${datacenter!}</td>
      </tr>
    </table>
  </div>
  <div class="col-md-6">
    <table id='instances' class="table table-condensed table-striped table-hover">
      <tr>
        <td>当前时间</td>
        <td>${currentTime}</td>
      </tr>
      <tr>
        <td>启动时长</td>
        <td>${upTime}</td>
      </tr>
      <tr>
        <td>租赁期满使能</td><!--是否处于没有续约就剔除服务状态(true表示是)，或者说是否处于自我保护状态（false表示是）
                                如在配置文件中关闭了自我保护机制，则永远是true，即永远不保护服务，没有续约即剔除。-->
        <td>${registry.leaseExpirationEnabled?c}</td>
      </tr>
      <tr>
        <td>续约阈值</td><!--EurekaServer每分钟期望收到的续约次数-->
        <td>${registry.numOfRenewsPerMinThreshold}</td>
      </tr>
      <tr>
        <td>续约次数(最近一分钟)</td>
        <td>${registry.numOfRenewsInLastMin}</td>
      </tr>
    </table>
  </div>
</div>

<#if isBelowRenewThresold>
    <#if !registry.selfPreservationModeEnabled>
        <h4 id="uptime"><font size="+1" color="red"> <b>
            <#--RENEWALS ARE LESSER THAN THE THRESHOLD. THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.-->
                最近一分钟内续约次数低于续约阈值，自我保存机制关闭，这可能无法在出现网络或其他问题时保护实例不被剔除。
            </b> </font></h4>
    <#else>
        <h4 id="uptime"><font size="+1" color="red"><b>
            <#--EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.-->
              紧急情况！最近一分钟内续约次数低于续约阈值，自我保存机制开启，EurekaServer不会剔除任何实例，直到自我保护机制关闭(当最近一分钟内续约次数不低于续约阈值时)。
        </b></font></h4>
    </#if>
<#elseif !registry.selfPreservationModeEnabled>
    <h4 id="uptime"><font size="+1" color="red"><b>
        <#--THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.-->
        自我保存机制关闭，这可能无法在出现网络或其他问题时保护实例不被剔除。
    </b></font></h4>
</#if>

<h1>副本</h1>
<ul class="list-group">
  <#list replicas as replica>
    <li class="list-group-item"><a href="${replica.value}">${replica.key}</a></li>
  </#list>
</ul>

