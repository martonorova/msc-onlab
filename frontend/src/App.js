import React from 'react';
import './App.css';
import { Layout } from 'antd';

import JobState from './context/job/JobState';
import Home from './components/pages/Home';

const { Header, Content, Footer } = Layout;

const App = () => {
  return (
    <JobState>
      <Layout >
        <Header>
        </Header>
        <Content style={{ padding: '20px 50px', display: 'flex', justifyContent: 'center'}}>
          <div className='site-layout-content'>
            <Home />
          </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>morova ©2020</Footer>
      </Layout>
    </JobState>
  );
};

export default App;
