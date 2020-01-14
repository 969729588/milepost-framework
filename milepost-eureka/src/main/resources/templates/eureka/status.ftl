<#import "/spring.ftl" as spring />
<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
  <head>
    <base href="<@spring.url basePath/>">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Eureka</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width">

    <link rel="stylesheet" href="eureka/css/wro.css">
    <style type="text/css">
        .clearDiv{
            height: 15px
        }
        .metadataDIV .itemDiv{
            padding-bottom: 3px
        }
        .metadataDIV .itemDiv span{
            /* background-color: #4FB548; */
            color: #135D0E;
            /* border: 2px solid #4FB548; */
            width: 100px;
        }

    </style>
  </head>

  <body id="one">
    <#include "header.ftl">
    <div class="container-fluid xd-container">
      <#include "navbar.ftl">
      <h1><#--Instances currently registered with Eureka-->
          已注册服务实例列表
      </h1>
      <table id='instances' class="table table-striped table-hover">
        <thead>
          <tr><th>应用</th><th>AMIs</th><th>可用地址</th><th>状态</th></tr>
        </thead>
        <tbody>
          <#if apps?has_content>
            <#list apps as app>
              <tr>
                <td><b>${app.name}</b></td>
                <td>
                  <#list app.amiCounts as amiCount>
                    <b>${amiCount.key}</b> (${amiCount.value})<#if amiCount_has_next>,</#if>
                  </#list>
                </td>
                <td>
                  <#list app.zoneCounts as zoneCount>
                    <b>${zoneCount.key}</b> (${zoneCount.value})<#if zoneCount_has_next>,</#if>
                  </#list>
                </td>
                <td>
                  <#list app.instanceInfos as instanceInfo>
                      <table>
                          <tr>
                              <td>
                                  <div style="margin-right: 30px;">
                                    <#if instanceInfo.isNotUp>
                                    <font color=red size=+1><b>
                                    </#if>
                                      <b>${instanceInfo.status}</b> (${instanceInfo.instances?size})
                                    <#if instanceInfo.isNotUp>
                                    </b>
                                    </font>
                                    </#if>
                                  </div>
                              </td>
                              <td>
                                  <div>
                                    <#list instanceInfo.instances as instance>
                                        <div>
                                          <#if instance.isHref>
                                              <a href="${instance.url}" target="_blank">${instance.id}(${instance.url})</a>
                                          <#else>
                                              ${instance.id}
                                          </#if>
                                            <!--租户、权重、标签、跟踪采样率-->
                                            <div name="metadataDIV" class="metadataDIV">
                                              <div class="itemDiv">
                                                  <span>租户：
                                                <#if instance.instanceInfoMap.metadata.tenant??>
                                                    ${instance.instanceInfoMap.metadata['tenant']}
                                                <#else>
                                                    未设置
                                                </#if>
                                                  </span>
                                                  <#--<button name="tenant" class="btn btn-default btn-xs" style="width:80px;">设置租户</button>-->
                                              </div>
                                              <div class="itemDiv">
                                                  <span>权重：
                                                <#if instance.instanceInfoMap.metadata.weight??>
                                                    ${instance.instanceInfoMap.metadata['weight']}
                                                <#else>
                                                    未设置
                                                </#if>
                                                  </span>
                                                  <#--<button name="weight" class="btn btn-default btn-xs" style="width:80px;">设置权重</button>-->
                                              </div>
                                              <div class="itemDiv">
                                                  <span>与标签：
                                                <#if instance.instanceInfoMap.metadata['label-and']??>
                                                    ${instance.instanceInfoMap.metadata['label-and']}
                                                <#else>
                                                    未设置
                                                </#if>
                                                  </span>
                                                  <#--<button name="label-and" class="btn btn-default btn-xs" style="width:80px;">设置与标签</button>-->
                                              </div>
                                              <div class="itemDiv">
                                                  <span>或标签：
                                                <#if instance.instanceInfoMap.metadata['label-or']??>
                                                    ${instance.instanceInfoMap.metadata['label-or']}
                                                <#else>
                                                    未设置
                                                </#if>
                                                  </span>
                                                  <#--<button name="label-or" class="btn btn-default btn-xs" style="width:80px;">设置或标签</button>-->
                                              </div>
                                              <#--暂时不考虑跟踪采样率-->
                                              <div class="itemDiv">
                                                  <span>跟踪采样率：
                                                <#if instance.instanceInfoMap.metadata['track-sampling']??>
                                                    ${instance.instanceInfoMap.metadata['track-sampling']}
                                                <#else>
                                                    未设置
                                                </#if>
                                                  </span>
                                                  <#--<button name="track-sampling" class="btn btn-default btn-xs" style="width:80px;">跟踪采样率</button>-->
                                              </div>
                                          </div>
                                          <#if instance_has_next><div class="clearDiv"></div></#if>
                                        </div>
                                    </#list>
                                  </div>
                              </td>
                          </tr>
                      </table>
                  </#list>
                </td>
              </tr>
            </#list>
          <#else>
            <tr><td colspan="4">无可用服务实例</td></tr>
          </#if>

        </tbody>
      </table>

      <h1>基本信息</h1>
      <!--节点不可达的原因：https://www.cnblogs.com/lonelyJay/p/9940199.html-->
      <table id='generalInfo' class="table table-striped table-hover">
        <thead>
          <tr><th>属性</th><th>值</th></tr>
        </thead>
        <tbody>
          <#list statusInfo.generalStats?keys as stat>
            <tr>
              <td>${stat}</td><td>${statusInfo.generalStats[stat]!""}</td>
            </tr>
          </#list>
          <#list statusInfo.applicationStats?keys as stat>
            <tr>
              <td>${stat}</td><td>${statusInfo.applicationStats[stat]!""}</td>
            </tr>
          </#list>
        </tbody>
      </table>

      <h1>实例信息</h1>

      <table id='instanceInfo' class="table table-striped table-hover">
        <thead>
          <tr><th>属性</th><th>值</th></tr>
        <thead>
        <tbody>
          <#list instanceInfo?keys as key>
            <tr>
              <td>${key}</td><td>${instanceInfo[key]!""}</td>
            </tr>
          </#list>
        </tbody>
      </table>
    </div>
    <script type="text/javascript" src="eureka/js/wro.js" ></script>
    <script type="text/javascript">
       $(document).ready(function() {
         $('table.stripeable tr:odd').addClass('odd');
         $('table.stripeable tr:even').addClass('even');

           $('div[name=metadataDIV] button').click(function(){
               var btn_text = $(this).text();
               //这是EurekaServer提供的更新元数据的rest接口，文档见https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
               var url = '/eureka/apps/'+ $(this).prev().attr('name') +'/metadata?'+$(this).attr('name')+'='+$(this).prev().val();
               $.ajax({
                   url: url,
                   type: 'PUT',
                   success: function (data, textStatus) {
                       if(textStatus == 'success'){
                           alert('更新成功');
                       }else{
                           alert('更新失败');
                       }
                   },
                   error: function (XMLHttpRequest, textStatus, errorThrown) {
                       alert('更新失败，' + textStatus);
                   }
               });
           });

       });
    </script>
  </body>
</html>
