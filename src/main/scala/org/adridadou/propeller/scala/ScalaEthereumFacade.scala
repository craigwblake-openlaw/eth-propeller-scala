package org.adridadou.propeller.scala

import org.adridadou.ethereum.propeller.EthereumFacade
import org.adridadou.ethereum.propeller.solidity.{SolidityContractDetails, SolidityEvent}
import org.adridadou.ethereum.propeller.swarm.SwarmHash
import org.adridadou.ethereum.propeller.values._
import rx.lang.scala.Observable

import scala.compat.java8.OptionConverters._
import rx.lang.scala.JavaConversions._
import scala.collection.JavaConverters._

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.runtime.BoxedUnit

/**
  * Created by davidroon on 18.04.17.
  * This code is released under Apache 2 license
  */
class ScalaEthereumFacade(facade:EthereumFacade, converter:ScalaFutureConverter) {
  def createContractProxy[T](abi: EthAbi, address: EthAddress, key: EthAccount)(implicit tag: ClassTag[T]):T = facade.createContractProxy(abi, address, key, tag.runtimeClass.asInstanceOf[Class[T]])
  def createContractProxy[T](contract: SolidityContractDetails, address: EthAddress, key: EthAccount)(implicit tag: ClassTag[T]):T = facade.createContractProxy(contract, address, key, tag.runtimeClass.asInstanceOf[Class[T]])
  def createContractProxy[T](address: EthAddress, account: EthAccount)(implicit tag: ClassTag[T]): T = facade.createContractProxy(address, account, tag.runtimeClass.asInstanceOf[Class[T]])

  def findEventDefinition[T](contract: SolidityContractDetails, eventName: String)(implicit tag: ClassTag[T]): Option[SolidityEvent[T]] = facade.findEventDefinition(contract,eventName, tag.runtimeClass.asInstanceOf[Class[T]]).asScala
  def findEventDefinition[T](abi: EthAbi, eventName: String)(implicit tag: ClassTag[T]): Option[SolidityEvent[T]] = facade.findEventDefinition(abi,eventName, tag.runtimeClass.asInstanceOf[Class[T]]).asScala
  def events():ScalaEthereumEventHandler = ScalaEthereumEventHandler(facade.events(), converter)
  def observeEvents[T](eventDefiniton: SolidityEvent[T], address: EthAddress): Observable[T] = facade.observeEvents(eventDefiniton, address)
  def compile(solidityCode: SoliditySourceFile):SCompilationResult = SCompilationResult(facade.compile(solidityCode))
  def getEventsAtBlock[T](eventDefinition:SolidityEvent[T], address:EthAddress, number:Long):Seq[T] = {
    facade.getEventsAt(number, eventDefinition, address).asScala
  }

  def getEventsAtBlock[T](eventDefinition:SolidityEvent[T], address:EthAddress, hash:EthHash):Seq[T] = {
    facade.getEventsAt(hash, eventDefinition, address).asScala
  }

  def publishContractWithValue(contract: SolidityContractDetails, account: EthAccount, value:EthValue, constructorArgs: AnyRef*): Future[EthAddress] = converter.convert(facade.publishContractWithValue(contract, account, value, constructorArgs:_*))
  def publishContract(contract: SolidityContractDetails, account: EthAccount, constructorArgs: AnyRef*): Future[EthAddress] = converter.convert(facade.publishContract(contract, account, constructorArgs:_*))
  def publishMetadataToSwarm(contract: SolidityContractDetails): SwarmHash = facade.publishMetadataToSwarm(contract)
  def sendEther(fromAccount: EthAccount, to: EthAddress, value: EthValue): Future[EthExecutionResult] = converter.convert(facade.sendEther(fromAccount, to, value))

  def addressExists(address: EthAddress): Boolean = facade.addressExists(address)

  def getBalance(addr: EthAddress): EthValue = facade.getBalance(addr)
  def getBalance(account: EthAccount): EthValue = facade.getBalance(account.getAddress)
  def getNonce(address: EthAddress): Nonce = facade.getNonce(address)
  def getCurrentBlockNumber: Long = facade.getCurrentBlockNumber

  def getCode(address: EthAddress): SmartContractByteCode = facade.getCode(address)

  def getMetadata(swarmMetadaLink: SwarmMetadaLink): SmartContractMetadata = facade.getMetadata(swarmMetadaLink)

}

object ScalaEthereumFacade {
  def apply(facade:EthereumFacade):ScalaEthereumFacade = {
    val converter = new ScalaFutureConverter()
    facade.addFutureConverter(converter)
    facade.addVoidType(classOf[BoxedUnit])
    facade.addVoidType(classOf[Unit])
    new ScalaEthereumFacade(facade, converter)
  }
}
