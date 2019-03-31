app.controller('indexController',function ($scope,indexServer) {
   $scope.showName=function () {
       indexServer.showName().success(function (response) {
           $scope.namee=response.namee;
           $scope.datee=response.datee;
       })
   }



})

