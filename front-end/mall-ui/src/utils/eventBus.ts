import mitt from 'mitt'

type Events = {}

const eventBus = mitt<Events>()

export default eventBus
