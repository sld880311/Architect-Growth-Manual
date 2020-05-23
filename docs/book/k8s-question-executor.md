<!-- TOC -->

- [K8S常用问题解决](#k8s%e5%b8%b8%e7%94%a8%e9%97%ae%e9%a2%98%e8%a7%a3%e5%86%b3)
  - [Back-off pulling image "registry.access.redhat.com/rhel7/pod-infrastructure:latest"](#back-off-pulling-image-%22registryaccessredhatcomrhel7pod-infrastructurelatest%22)

<!-- /TOC -->

# K8S常用问题解决

## Back-off pulling image "registry.access.redhat.com/rhel7/pod-infrastructure:latest"

```bash
yum install *rhsm*

wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm

rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout ./etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem
```