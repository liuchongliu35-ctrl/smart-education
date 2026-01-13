import styled,{keyframes} from 'styled-components'

const move = keyframes`
 
    0% { transform: translateY(0); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0); }
  
`

const DotBase = styled.div`
  width: 10px;
  height: 10px;
  background-color: black;
  border-radius: 50%;
  animation: ${move} 1.5s infinite;
`;

const Dot1 = styled(DotBase)`
  animation-delay: 0s;
`;

const Dot2 = styled(DotBase)`
  margin-left: 20px;
  animation-delay: 0.5s;
`;

const Dot3 = styled(DotBase)`
  margin-left: 20px;
  animation-delay: 1s;
`;

const WaitingDot = () =>{


    


    return(
        <>
        <div style={{display:'flex',width:'max-content',margin:'auto',marginTop:20}}> 
        <Dot1></Dot1>
        <Dot2></Dot2>
        <Dot3></Dot3>
        </div>
        </>
    )
}

export default WaitingDot