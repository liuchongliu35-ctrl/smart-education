import { hatch } from 'ldrs'
hatch.register()

// Default values shown




const ModalLoadingComponent = () =>(
    <div style={{width:'max-content',display:'flex',flexDirection:'column',justifyContent:'center'}}>
        <l-hatch size="28" stroke="4" speed="3.5"  color="#435fff" ></l-hatch>
    </div>
)

export default ModalLoadingComponent