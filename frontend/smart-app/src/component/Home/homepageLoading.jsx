
const HomePageLoading = () =>{

  return(
    <Layout style={layoutStyle}>
    <Layout>
      <Header className={style.header}>
      </Header>
      <Content style={contentStyle} className={style.content}>
      <Skeleton active />
      <br />
      <Skeleton active />
      </Content>
    </Layout>
  </Layout>
  )
}

export default HomePageLoading